package com.vasrask.boubou;

import androidx.room.TypeConverter;
import com.vasrask.boubou.entities.BabyActivityType;
import java.util.Date;

public class Converters {

    @TypeConverter
    public static BabyActivityType fromString(String value) {
        return BabyActivityType.valueOf(value);
    }

    @TypeConverter
    public static String toString(BabyActivityType category) {
        return category.name();
    }


    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
