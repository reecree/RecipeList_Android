package com.rupert.recipelist;

import android.content.Context;

import java.io.File;
import java.io.IOException;

public class Globals{

    public final static String APP_ID = "4912766377932110327";

    public final static String BOARD_NAME_KEY = "boardName";
    public final static String BOARD_ID_KEY = "boardId";
    public final static String INGREDIENT_KEY = "ingrArray";


    public final static String EMPTY_JSON = "{}";

    public final static String ACCESS_TOKEN_FILE_NAME = "atPinFile";

    public static void RemoveAccessToken(Context context) {
        File dir = context.getFilesDir();
        File file = new File(dir, ACCESS_TOKEN_FILE_NAME);
        if(file.exists())
            file.delete();
    }
}
