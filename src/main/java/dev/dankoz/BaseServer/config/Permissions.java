package dev.dankoz.BaseServer.config;

public enum Permissions {
    PERMISSION_BASIC("PERMISSION_BASIC");


    private final String name;

    Permissions(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }
}
