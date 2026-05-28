package com.sergio.retrodex;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "retrodex.db";
    private static final int DB_VERSION = 5;

    public static final String TABLE = "characters";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_DECADE = "decade";
    public static final String COL_CATEGORY = "category";
    public static final String COL_ORIGIN = "origin";
    public static final String COL_IMAGE_PATH = "image_path";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_NAME + " TEXT NOT NULL, " +
                    COL_DESCRIPTION + " TEXT, " +
                    COL_DECADE + " TEXT, " +
                    COL_CATEGORY + " TEXT, " +
                    COL_ORIGIN + " TEXT, " +
                    COL_IMAGE_PATH + " TEXT" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        insertPreloadedData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 5) {
            migrateToVersion5(db);
        }
    }

    public long insert(Character character) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = toContentValues(character);
        long id = db.insert(TABLE, null, cv);
        db.close();
        return id;
    }

    public int update(Character character) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = toContentValues(character);
        int rows = db.update(TABLE, cv, COL_ID + "=?",
                new String[]{String.valueOf(character.getId())});
        db.close();
        return rows;
    }

    public int delete(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE, COL_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public Character getById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE, null,
                COL_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);
        Character character = null;
        if (cursor.moveToFirst()) {
            character = fromCursor(cursor);
        }
        cursor.close();
        db.close();
        return character;
    }

    public List<Character> getAll(String orderBy) {
        return query(null, null, orderBy);
    }

    public List<Character> getByDecade(String decade, String orderBy) {
        return query(COL_DECADE + "=?", new String[]{decade}, orderBy);
    }

    public List<Character> getByCategory(String category, String orderBy) {
        return query(COL_CATEGORY + "=?", new String[]{category}, orderBy);
    }

    private List<Character> query(String selection, String[] args, String orderBy) {
        List<Character> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE, null, selection, args, null, null, resolveOrder(orderBy));
        while (cursor.moveToNext()) {
            list.add(fromCursor(cursor));
        }
        cursor.close();
        db.close();
        return list;
    }

    private String resolveOrder(String pref) {
        if (pref == null) return COL_NAME + " ASC";
        switch (pref) {
            case "name_desc":
                return COL_NAME + " DESC";
            case "decade":
                return "CASE " + COL_DECADE +
                        " WHEN '70s' THEN 1" +
                        " WHEN '80s' THEN 2" +
                        " WHEN '90s' THEN 3" +
                        " WHEN '2000s' THEN 4" +
                        " ELSE 5 END ASC, " + COL_NAME + " ASC";
            default:
                return COL_NAME + " ASC";
        }
    }

    private ContentValues toContentValues(Character character) {
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, character.getName());
        cv.put(COL_DESCRIPTION, character.getDescription());
        cv.put(COL_DECADE, character.getDecade());
        cv.put(COL_CATEGORY, character.getCategory());
        cv.put(COL_ORIGIN, character.getOrigin());
        cv.put(COL_IMAGE_PATH, character.getImagePath());
        return cv;
    }

    private Character fromCursor(Cursor cursor) {
        return new Character(
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_DECADE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_ORIGIN)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE_PATH))
        );
    }

    private void insertPreloadedData(SQLiteDatabase db) {
        insert(db, "Space Invader", "El alienígena pixelado más famoso de la historia. Su avance implacable en filas descendentes definió el género del shoot 'em up y se convirtió en el símbolo de los arcades de los 70.", "70s", "Videojuego", "Space Invaders (1978)");
        insert(db, "Mazinger Z", "El primer súper robot pilotado por un humano. Koji Kabuto al mando de este coloso de hierro estableció las bases del género mecha que definiría décadas de anime.", "70s", "Anime", "Mazinger Z (1972)");
        insert(db, "Heidi", "La niña de los Alpes que conquistó corazones en todo el mundo. Su historia de amistad e inocencia en las montañas suizas marcó la infancia de toda una generación.", "70s", "Anime", "Heidi (1974)");
        insert(db, "Speedy Gonzales", "¡Ándale, ándale! El ratón más veloz de México nunca fue atrapado. Símbolo pop de la cultura latina, su velocidad y picardía le ganaron un lugar eterno en la animación clásica.", "70s", "Animación", "Looney Tunes (1953)");

        insert(db, "Mario", "El fontanero más famoso del mundo no necesita presentación. Desde los tubos de la Mushroom Kingdom hasta las galaxias, Mario ha salvado a la Princesa Peach en más de 200 aventuras a lo largo de cuatro décadas.", "80s", "Videojuego", "Super Mario Bros (1985)");
        insert(db, "Pac-Man", "Come-cocos insaciable que devoró el mercado de los arcades. Creado por Namco en 1980, se convirtió en el videojuego más reconocido de todos los tiempos y en un icono cultural global.", "80s", "Videojuego", "Pac-Man (1980)");
        insert(db, "He-Man", "¡Por el poder de Grayskull! El hombre más poderoso del universo empuñó su espada mágica y se convirtió en el ídolo de los niños de los 80, vendiendo millones de juguetes en todo el mundo.", "80s", "Animación", "Masters of the Universe (1983)");
        insert(db, "Link", "El héroe del tiempo que protege Hyrule. En silencio pero con valor inquebrantable, Link ha viajado por dungeons, templos y reinos paralelos durante cuatro décadas sin pronunciar una sola palabra.", "80s", "Videojuego", "The Legend of Zelda (1986)");
        insert(db, "Mega Man", "El robot azul del Dr. Light que lucha por la paz. Su revolucionario sistema de copiar los poderes de los jefes vencidos lo convirtió en un icono del diseño de videojuegos de los 80.", "80s", "Videojuego", "Mega Man (1987)");
        insert(db, "Mickey Mouse", "El ratón más famoso de la animación clásica. Con su carisma eterno y su legado histórico, Mickey se convirtió en uno de los iconos más reconocibles de la cultura popular mundial.", "80s", "Animación", "Disney");

        insert(db, "Sonic", "¡Gotta go fast! El erizo azul de SEGA que desafió a Mario en velocidad. Con su actitud rebelde, sus zapatillas rojas y su velocidad supersónica, Sonic se convirtió en la mascota más cool de los 90.", "90s", "Videojuego", "Sonic the Hedgehog (1991)");
        insert(db, "Goku", "El guerrero Saiyan más puro de corazón. Desde sus aventuras en Dragon Ball hasta las batallas cósmicas de Dragon Ball Z, Goku ha redefinido lo que significa superar los propios límites hasta el infinito.", "90s", "Anime", "Dragon Ball Z (1989)");
        insert(db, "Bart Simpson", "El niño problemático de Springfield que revolucionó la animación adulta. Sus travesuras, su skate y su célebre 'Ay, caramba!' lo hicieron el anti-héroe favorito de toda una generación.", "90s", "Animación", "Los Simpson (1989)");
        insert(db, "Lara Croft", "La arqueóloga aventurera que cambió los videojuegos. Armada con dos pistolas y determinación inagotable, Lara Croft exploró tumbas imposibles y rompió barreras de género en la industria del gaming.", "90s", "Videojuego", "Tomb Raider (1996)");
        insert(db, "Ash Ketchum", "Un chico de Pueblo Paleta con el sueño de convertirse en Maestro Pokémon. Junto a Pikachu, Ash viajó por todas las regiones del mundo Pokémon durante 25 años de aventuras interminables.", "90s", "Anime", "Pokémon (1997)");
        insert(db, "Scooby-Doo", "El gran danés más querido del misterio animado. Entre sustos, bocadillos gigantes y persecuciones imposibles, Scooby-Doo lleva décadas resolviendo enigmas con su pandilla.", "90s", "Animación", "Scooby-Doo, Where Are You!");

        insert(db, "Master Chief", "El Spartan-117, último bastión de la humanidad contra el Covenant. Dentro de su armadura Mjolnir verde, el Jefe Maestro define lo que significa ser un héroe militar en la era de los videojuegos modernos.", "2000s", "Videojuego", "Halo (2001)");
        insert(db, "Naruto", "El shinobi con el Zorro de Nueve Colas sellado en su interior. Naruto Uzumaki pasó de ser el paria de Konoha a convertirse en el Séptimo Hokage, inspirando a millones de personas en todo el mundo.", "2000s", "Anime", "Naruto (2002)");
        insert(db, "Bob Esponja", "¡Él vive en una piña bajo el mar! La esponja más optimista de Fondo de Bikini se convirtió en el personaje de animación más popular de los 2000s y en el rey indiscutible de los memes de internet.", "2000s", "Animación", "Bob Esponja (1999)");
        insert(db, "Kratos", "El Fantasma de Esparta, antiguo dios de la guerra. Su furia implacable y su sed de venganza contra los dioses del Olimpo lo convirtieron en uno de los protagonistas más intensos del gaming moderno.", "2000s", "Videojuego", "God of War (2005)");
        insert(db, "Shrek", "El ogro verde que no necesita ser bonito para ser el héroe. Su humor irreverente, sus capas como las cebollas y su final inesperado hicieron de Shrek un clásico moderno e indiscutible de la animación mundial.", "2000s", "Animación", "Shrek (2001)");
    }

    private void insert(SQLiteDatabase db, String name, String desc,
                        String decade, String category, String origin) {
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, name);
        cv.put(COL_DESCRIPTION, desc);
        cv.put(COL_DECADE, decade);
        cv.put(COL_CATEGORY, category);
        cv.put(COL_ORIGIN, origin);
        cv.put(COL_IMAGE_PATH, CharacterImageHelper.getPresetToken(name));
        db.insert(TABLE, null, cv);
    }

    private void migrateToVersion5(SQLiteDatabase db) {
        db.delete(TABLE, COL_NAME + "=?", new String[]{"Pong"});

        String[] names = new String[]{
                "Space Invader", "Mazinger Z", "Heidi", "Speedy Gonzales",
                "Mario", "Pac-Man", "He-Man", "Link", "Mega Man",
                "Sonic", "Goku", "Bart Simpson", "Lara Croft", "Ash Ketchum",
                "Master Chief", "Naruto", "Bob Esponja", "Kratos", "Shrek",
                "Mickey Mouse", "Scooby-Doo"
        };

        for (String name : names) {
            String assetToken = CharacterImageHelper.getPresetToken(name);
            ContentValues values = new ContentValues();
            values.put(COL_IMAGE_PATH, assetToken);
            db.update(
                    TABLE,
                    values,
                    COL_NAME + "=? AND (" + COL_IMAGE_PATH + " IS NULL OR " + COL_IMAGE_PATH + "='' OR " + COL_IMAGE_PATH + " LIKE 'preset:%' OR " + COL_IMAGE_PATH + " LIKE 'asset:preloaded_characters/%.jpg')",
                    new String[]{name}
            );
        }

        ensureCharacterExists(db,
                "Mickey Mouse",
                "El ratón más famoso de la animación clásica. Con su carisma eterno y su legado histórico, Mickey se convirtió en uno de los iconos más reconocibles de la cultura popular mundial.",
                "80s",
                "Animación",
                "Disney");

        ensureCharacterExists(db,
                "Scooby-Doo",
                "El gran danés más querido del misterio animado. Entre sustos, bocadillos gigantes y persecuciones imposibles, Scooby-Doo lleva décadas resolviendo enigmas con su pandilla.",
                "90s",
                "Animación",
                "Scooby-Doo, Where Are You!");
    }

    private void ensureCharacterExists(SQLiteDatabase db,
                                       String name,
                                       String description,
                                       String decade,
                                       String category,
                                       String origin) {
        Cursor cursor = db.query(
                TABLE,
                new String[]{COL_ID},
                COL_NAME + "=?",
                new String[]{name},
                null,
                null,
                null
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();

        if (!exists) {
            insert(db, name, description, decade, category, origin);
        }
    }
}
