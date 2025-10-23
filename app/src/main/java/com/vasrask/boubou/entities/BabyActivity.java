package com.vasrask.boubou.entities;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.Timestamp;
import java.util.Objects;


public class BabyActivity {
    private final String TAG = "BabyActivity";

    private String id;
    private double duration;
    private boolean diaper_check;
    private boolean medicine_check;
    private double intake;
    private String userId;
    private Timestamp timestamp;
    private String notes;
    private BabyActivityType babyActivityType;
    private FeedingType feedingType;
    public BabyActivity() {}

    public BabyActivity(String id, String userId, double amount, BabyActivityType category, FeedingType feedingType, String notes) {
        this.id = id;
        this.userId = userId;
        this.babyActivityType = category;
        this.feedingType = feedingType;
        if (this.babyActivityType == BabyActivityType.SLEEP || this.babyActivityType == BabyActivityType.PLAYTIME) {
            this.duration = amount;
        } else if (this.babyActivityType == BabyActivityType.FEEDING) {
             if (this.feedingType == FeedingType.BREASTFEEDING) {
                 this.duration = amount;
             } else {
                 this.intake = amount;
             }
        }
        this.notes = notes;
        this.timestamp = null;
    }

    public BabyActivity(String id, String userId, boolean check, BabyActivityType category, String notes) {
        this.id = id;
        this.userId = userId;
        this.babyActivityType = category;
        if (this.babyActivityType == BabyActivityType.DIAPER_CHANGE) {
            this.diaper_check = check;
        } else if (this.babyActivityType == BabyActivityType.MEDICINE) {
            this.medicine_check = check;
        }
        this.notes = notes;
        this.timestamp = null;
    }
    public String getBabyActivityTypeString(Context context) {
        return context.getString(babyActivityType.getId());
    }

    public String getFeedingTypeString(Context context) {
        return context.getString(feedingType.getId());
    }
    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getIntake() {
        return intake;
    }

    public void setIntake(double intake) {
        this.intake = intake;
    }

    public boolean getDiaper_check() {
        return diaper_check;
    }

    public void setDiaper_check(boolean check) {
        this.diaper_check = check;
    }

    public boolean getMedicine_check() {
        return medicine_check;
    }

    public void setMedicine_check(boolean check) {
        this.medicine_check = check;
    }

    public BabyActivityType getBaby_activity_type() {
        return babyActivityType;
    }

    public void setBaby_activity_type(BabyActivityType babyActivityType) {
        this.babyActivityType = babyActivityType;
    }


    public FeedingType getFeeding_type() {
        return feedingType;
    }

    public void setFeeding_type(FeedingType feedingType) {
        this.feedingType = feedingType;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        BabyActivity that = (BabyActivity) obj;
        return id.equals(that.id) && userId.equals(that.userId) && Objects.equals(babyActivityType, that.babyActivityType) && Objects.equals(timestamp, that.timestamp) && duration == that.duration;
    }

  @NonNull
  @Override
  public String toString() {
      StringBuilder sb = new StringBuilder("BabyActivity{");
      sb.append("id='").append(id).append('\'')
              .append(", babyActivityType='").append(String.format("%s",babyActivityType)).append('\'');
      switch (babyActivityType) {
          case SLEEP:
          case PLAYTIME:
              sb.append(", duration='").append(duration)
                      .append("m").append('\'');
              break;
          case FEEDING:
              if (feedingType == FeedingType.BREASTFEEDING) {
                  sb.append(", duration='").append(duration)
                          .append("m").append('\'');
              } else {
                  sb.append(", intake='").append(intake)
                          .append("ml").append('\'');
              }
              sb.append(", feedingType='").append(String.format("%s",feedingType)).append('\'');
              break;
          case DIAPER_CHANGE:
              if (diaper_check) {
                  sb.append(", diapered").append('\'');
              } else {
                  sb.append(", Did not change diaper").append('\'');
              }
              break;
          case MEDICINE:
              if (medicine_check) {
                  sb.append(", took medicine");
              } else {
                  sb.append(", did not take medicine");
              }
              break;
      }


      if (notes != null && !notes.isEmpty()) {
          sb.append(", notes='").append(notes).append('\'');
      }
      sb.append(", userId='").append(userId).append('\'')
              .append(", timestamp=").append(timestamp)
              .append('}');

      return sb.toString();
  }
}
