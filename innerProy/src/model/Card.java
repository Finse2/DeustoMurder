package model;

import java.io.Serializable;
import java.nio.file.Path;

public abstract class Card implements Serializable {

    private static final long serialVersionUID = 1L;

    private Path aspectToRender;
    private String nameToRender;

    public Card(Path aspectToRender,  String nameToRender) {
        this.aspectToRender = aspectToRender;
        this.nameToRender = nameToRender;

    }

    public Card(){}

    public Path getAspectToRender() {
        return aspectToRender;
    }

    public void setAspectToRender(Path aspectToRender) {
        this.aspectToRender = aspectToRender;
    }

    public String getNameToRender() {
        return nameToRender;
    }

    public void setNameToRender(String nameToRender) {
        this.nameToRender = nameToRender;
    }
}
