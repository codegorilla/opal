package org.opal;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;

public class Reader {

    Path input = null;

    public Reader (Path input) {
        this.input = input;
    }

    public String process () {

        String content = null;
        try {
            content = Files.readString(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }
}
