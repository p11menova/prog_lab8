package org.example.commandsManager;

import org.example.details.StorageOfManagers;
import org.example.exceptions.ExecuteException;
import org.example.island.commands.*;
import org.example.island.details.exceptions.IllegalValueException;
import org.example.island.details.exceptions.NoSuchCommandException;
import org.example.island.object.*;
import org.example.requests.RequestsManager;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;



public class ExecuteManager {
    private Map<String, Command> commandRegistry = new HashMap<>();
    private Map<Object, String> commandEx = new HashMap<>();
    private ArrayList<String> commandList = new ArrayList<>();

    public ExecuteManager(){
        addCommand("executeShow", new Show());
        addCommand("executeInfo", new Info());
        addCommand("executeInsert", new InsertNull());
        addCommand("executeUpdate", new UpdateId());
        addCommand("executeRemove", new Remove_key());
        addCommand("executeClear", new Clear());
        addCommand("executeScript", new Execute_script());
        addCommand("executeRemoveGreater", new Remove_greater());
        addCommand("executeRemoveLower", new Remove_lower_key());
        addCommand("executeHistory", new History());
        addCommand("executeCountEdu", new Count_less_than_form_of_education());
        addCommand("executeFilterStudentsCount", new Filter_by_students_count());
        addCommand("executeFilterEdu", new Filter_less_than_form_of_education());
        addCommand("executeExit", new Exit());
        addCommand("executeHelp", new Help());
        addCommand("executeMessage", new Message());
        addCommand("executeSave", new Save());
    }

    public Map<String, Command> getCommandRegistry() {
        return commandRegistry;
    }
    public void commandExecute(Command cmd){
        try {
            Method mth;
            if(cmd.getArgumentCount() > 0){
                mth = this.getClass().getMethod(commandEx.get(cmd.getClass()), cmd.getArguments().getClass());
                mth.invoke(this, cmd.getArguments());
            }
            else{
                mth = this.getClass().getMethod(commandEx.get(cmd.getClass()));
                mth.invoke(this);
            }

        } catch (NoSuchMethodException  | IllegalAccessException | InvocationTargetException e) {
            RequestsManager.manager.answerForming("Во время выполнения команды произошла ошибка\nНе найден метод для исполнения данной команды");
        }


    }

    public void executeHelp(){
        Map<String, Command> map = getCommandRegistry();
        for(Command command : map.values()){
            RequestsManager.manager.answerForming(command.getName() + " - " + command.getDescription());
        }
    }

    public ArrayList<String> getCommandList() {
        return commandList;
    }

    public void executeHistory(){
        ArrayList<String> list = getCommandList();
        if(list.size() < 14){
            RequestsManager.manager.answerForming("Количества исполненных команд не достаточно для вывода истории");
        }
        else{
            for (int i = 0; i < 14; i++) {
                RequestsManager.manager.answerForming(list.get(i));
            }
        }
    }
    public void executeCLear(){
        String answer = StorageOfManagers.storage.clear();
        RequestsManager.manager.answerForming(answer);
    }
    public void executeCountEdu(ArrayList<Object> args){
        FormOfEducation form = (FormOfEducation) args.get(0);
        long count = StorageOfManagers.storage.getValues().stream().filter(group -> group.getFormOfEducation().compareTo(form) < 0).count();
        String answer = "Поле FormOfEducation меньше заданного вами значения у " + count + " элементов";
        RequestsManager.manager.answerForming(answer);
    }

    public void executeScript(ArrayList<Object> args){
        FileInputStream stream = null;
        try {
            stream = new FileInputStream((String) args.get(0));
        } catch (FileNotFoundException e) {
            RequestsManager.manager.answerForming("Файл который вы указали не найден");
            return;
        }
        if(!Execute_script.files.contains((String) args.get(0))){
            Execute_script.files.add((String) args.get(0));
            StorageOfManagers.fileSystem.parseScript(stream);
            RequestsManager.manager.answerForming("Выполнение скрипта завершено");
        }
        else{
            RequestsManager.manager.answerForming("Не не, слишком бесконечно получается");
        }
    }
    public void executeFilterStudentsCount(ArrayList<Object> args){
        List<String> list = new ArrayList<>();
        long studentCount;
        long count = 0;
        try{
            studentCount = Long.parseLong((String) args.get(0));
            count = StorageOfManagers.storage.getValues().stream().filter(group -> group.getStudentsCount() == studentCount).map(group -> list.add(group.toString())).count();
        }catch(NumberFormatException e){
            RequestsManager.manager.answerForming("Не удалось спарсить переданные вами данные");
        }

        if(count == 0){
            RequestsManager.manager.answerForming("В ходе выполнения программы совпадений не выявлено");
        }
        else{
            RequestsManager.manager.answerForming(list.toArray());
        }
    }
    public void executeFilterEdu(ArrayList<Object> args){
        FormOfEducation form;
        if(args.get(0).getClass() == String.class){
            form = FormOfEducation.getForm((String) args.get(0));
        }
        else{
            form = (FormOfEducation) args.get(0);
        }
        List<String> list = new ArrayList<>();
        long count = StorageOfManagers.storage.getValues().stream().filter(group -> group.getFormOfEducation().compareTo(form) < 0).count();
        if(count == 0){
            RequestsManager.manager.answerForming("Элементов, у которых значение поля FormOfEducation меньше заданного вами значения не нашлось");
        }
        else{
            RequestsManager.manager.answerForming(list.toArray());
        }
    }
    public void executeInfo(){
        RequestsManager.manager.answerForming(StorageOfManagers.storage.toString());
    }
    public void executeInsert(ArrayList<Object> args){
        StudyGroup obj = (StudyGroup) args.get(1);
        Integer key = (Integer) args.get(0);
        StorageOfManagers.storage.putWithKey(key, obj);
        StorageOfManagers.storage.putMapKeys(key, obj.getId());
        RequestsManager.manager.answerForming("Объект успешно добавлен");
    }

    public void executeRemoveGreater(ArrayList<Object> args){
        StudyGroup el = (StudyGroup) args.get(0);
        long count = StorageOfManagers.storage.getValues().stream().filter(group -> el.compareTo(group) > 0).map(group -> StorageOfManagers.storage.removeElement(StorageOfManagers.storage.findKey(group.getId()))).count();
        RequestsManager.manager.answerForming("В ходе исполнения команды было удалено " + count + " объектов");
    }

    public void executeRemove(ArrayList<Object> args){
        int key = 0;
        try{
            key = (Integer) args.get(0);
        }catch (NumberFormatException e){
            RequestsManager.manager.answerForming("Не удалось спарсить переданные вами данные");
        }
        if(StorageOfManagers.storage.removeElement(key)){
            RequestsManager.manager.answerForming("Объект успешно удалён");
        }
        else{
            RequestsManager.manager.answerForming("Объект, соответсвующий введённому вами ключу в коллекции не найден");
        }
    }
    public void executeRemoveLower(ArrayList<Object> args){
        ArrayList<Integer> keys = new ArrayList<>();
        int key;
        try{
            key = Integer.parseInt((String) args.get(0));
            long i = StorageOfManagers.storage.getKeys().stream().filter(key_i -> key > key_i).map(keys::add).count();
        }catch(NumberFormatException e){
            RequestsManager.manager.answerForming("Не удалось спарсить переданные вами данные");
        }
        if(keys.size() == StorageOfManagers.storage.getSize()){
            RequestsManager.manager.answerForming("Введённый вами ключ меньше всех ключей, что есть в коллекции, удаление элементов не было произведено");
        }
        else{
            long count = keys.stream().map(StorageOfManagers.storage::removeElement).count();
            RequestsManager.manager.answerForming("В ходе исполнения команды было удалено " + count + " объектов");
        }
    }
    public void executeSave(){
        StorageOfManagers.fileSystem.parseToFile(StorageOfManagers.storage.getMap());
    }
    public void executeExit(){
        executeSave();
        RequestsManager.manager.answerForming("Всего доброго!");
    }

    public void executeMessage(ArrayList<Object> listArg){
        String data = (String) listArg.get(0);
        if(data.equals("Пустая коллекция")){
            StorageOfManagers.storage.mapInit(new LinkedHashMap<>());
            RequestsManager.manager.answerForming("Коллекция инициализирована");
        }
        else if(data.equals("Завершение")){
            executeSave();
            RequestsManager.manager.answerForming("Всего доброго!");
        }
        else{
            try {
                StorageOfManagers.storage.mapInit(StorageOfManagers.fileSystem.parseToList(data));
                RequestsManager.manager.answerForming("Коллекция инициализирована");
            } catch (IOException e) {
                RequestsManager.manager.answerForming("Произошла ошибка при инициализации коллекции");
            }
        }
    }
    public void executeShow(){
        List<String> list = new ArrayList<>();
        StorageOfManagers.storage.getValue().stream().forEach(group -> list.add(group.toString()));
        if(!list.isEmpty()){
            RequestsManager.manager.answerForming(list.toArray());
        }
        else{
            RequestsManager.manager.answerForming("В коллекции пока нет элементов");
        }

    }
    public void executeUpdate(ArrayList<Object> args){
        StudyGroup element = (StudyGroup) args.get(1);
        Integer id = (Integer) args.get(0);
        element.setId(id);
        StorageOfManagers.storage.replaceElement(id, element);
        RequestsManager.manager.answerForming("Объект успешно заменён");
    }
    public void insertFormScript(ArrayList<String> args){
        try{
            int key = Integer.parseInt(args.get(0));
            long studentsCount = Long.parseLong(args.get(2));
            long shouldBeExpelled = Long.parseLong(args.get(3));
            FormOfEducation form = FormOfEducation.getForm(args.get(4));
            Semester sem = Semester.getSem(args.get(5));
            float coordinatesX = Float.parseFloat(args.get(6));
            double coordinatesY = Double.parseDouble(args.get(7));
            Float height = Float.parseFloat(args.get(9));
            double weight = Double.parseDouble(args.get(10));
            Color color = Color.getColor(args.get(11));
            Long x = Long.parseLong(args.get(12));
            long y = Long.parseLong(args.get(14));
            int z = Integer.parseInt(args.get(15));
            Location loc = new Location(x, y, z, args.get(13));
            Person admin = new Person(args.get(8), height, weight, color, loc);
            Coordinates coord = new Coordinates(coordinatesX, coordinatesY);
            StudyGroup el = new StudyGroup(args.get(1), studentsCount, shouldBeExpelled, coord, form, sem, admin);
            StorageOfManagers.storage.putWithKey(key, el);
        }catch(NumberFormatException | IllegalValueException e){
            RequestsManager.manager.answerForming("Значения команды insert  в скрипте не валидны");
        }
        finally{
            commandList.add("insert");
        }
    }
    public void updateFromScript(ArrayList<String> args){
        try{
            int id = Integer.parseInt(args.get(0));
            long studentsCount = Long.parseLong(args.get(2));
            long shouldBeExpelled = Long.parseLong(args.get(3));
            FormOfEducation form = FormOfEducation.getForm(args.get(4));
            Semester sem = Semester.getSem(args.get(5));
            float coordinatesX = Float.parseFloat(args.get(6));
            double coordinatesY = Double.parseDouble(args.get(7));
            Float height = Float.parseFloat(args.get(9));
            double weight = Double.parseDouble(args.get(10));
            Color color = Color.getColor(args.get(11));
            Long x = Long.parseLong(args.get(12));
            long y = Long.parseLong(args.get(14));
            int z = Integer.parseInt(args.get(15));
            Location loc = new Location(x, y, z, args.get(13));
            Person admin = new Person(args.get(8), height, weight, color, loc);
            Coordinates coord = new Coordinates(coordinatesX, coordinatesY);
            StudyGroup el = new StudyGroup(args.get(1), studentsCount, shouldBeExpelled, coord, form, sem, admin);
            el.setId(id);
            RequestsManager.manager.answerForming(StorageOfManagers.storage.replaceElement(id, el));
        }catch(NumberFormatException | IllegalValueException e){
            RequestsManager.manager.answerForming("Значения команды update в скрипте не валидны");
        }
        finally{
            commandList.add("update");
        }
    }

    public void commandExecute(String s)  {
        String[] str = parseCommand(s);
        Command command = null;
        try {
            command = getCommand(str[0].toLowerCase());
        } catch (NoSuchCommandException e) {
            RequestsManager.manager.answerForming("Сервер не смог распознать команду");
        }
        commandList.add(str[0]);
        String[] args = Arrays.copyOfRange(str, 1, str.length);
        command.setArguments(args);
        commandExecute(command);
    }

    public String[] parseCommand(String s){
        return s.split(" ");
    }


    public void addCommand(String method, Command cmd){
        commandEx.put(cmd.getClass(), method);
        commandRegistry.put(cmd.getName(), cmd);
    }

    public Command getCommand(String s) throws NoSuchCommandException {
        if(!commandRegistry.containsKey(s)){
            RequestsManager.manager.answerForming("Команда не найдена, повторите ввод");
        }
        return commandRegistry.getOrDefault(s, null);
    }

}
