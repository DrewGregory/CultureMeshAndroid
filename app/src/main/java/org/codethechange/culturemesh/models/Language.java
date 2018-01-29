package org.codethechange.culturemesh.models;

import java.io.Serializable;
import java.math.BigInteger;


/**
 * Created by nathaniel on 11/10/17.
 */

public class Language implements Serializable {
    private BigInteger id;

    private String name;

    public Language(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
