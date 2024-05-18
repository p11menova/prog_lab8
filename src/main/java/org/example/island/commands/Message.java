package org.example.island.commands;


import org.example.island.details.exceptions.NoSuchCommandException;

import java.util.ArrayList;

public class Message extends Command{


    public Message() {
        super("message", "Отправить текстовое сообщение");
        argumentCount = 1;
    }


    @Override
    public Command clientExecute(Object[] args) throws NoSuchCommandException {
        this.setArguments(args);
        return this;
    }

}
