package com.vasrask.boubou.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.Timestamp;
import java.util.Objects;


public class BabyActivity {
    private final String TAG = "BabyActivity";

    private String id;
    private double duration;
    private boolean poop_check;
    private boolean vitamin_check;

    private double intake;
    private String category;
    private String userId;
    private Timestamp timestamp;
    private String notes;
    private BabyActivityType babyActivityType;

    public BabyActivity() {}

    public BabyActivity(String id, String userId, double amount, BabyActivityType category, String notes) {
        this.id = id;
        this.userId = userId;
        this.babyActivityType = category;
        this.category = category.name();
        if (this.babyActivityType == BabyActivityType.SLEEPING || this.babyActivityType == BabyActivityType.EXERCISING) {
            this.duration = amount;
        } else if (this.babyActivityType == BabyActivityType.EATING) {
            this.intake = amount;
        }
        this.notes = notes;
        this.timestamp = null;
    }

    public BabyActivity(String id, String userId, boolean check, BabyActivityType category, String notes) {
        this.id = id;
        this.userId = userId;
        this.babyActivityType = category;
        this.category = category.name();
        if (this.babyActivityType == BabyActivityType.POOPING) {
            this.poop_check = check;
        } else if (this.babyActivityType == BabyActivityType.VITAMINS) {
            this.vitamin_check = check;
        }
        this.notes = notes;
        this.timestamp = null;
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

    public boolean getPoop_check() {
        return poop_check;
    }

    public void setPoop_check(boolean check) {
        this.poop_check = check;
    }

    public boolean getVitamin_check() {
        return vitamin_check;
    }

    public void setVitamin_check(boolean check) {
        this.vitamin_check = check;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BabyActivityType getBabyActivityType() {
        return babyActivityType;
    }

    public void setBabyActivityType(BabyActivityType babyActivityType) {
        this.babyActivityType = babyActivityType;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        BabyActivity that = (BabyActivity) obj;
        return id.equals(that.id) && userId.equals(that.userId) && Objects.equals(category, that.category) && Objects.equals(timestamp, that.timestamp) && duration == that.duration;
    }

  @NonNull
  @Override
  public String toString() {
      StringBuilder sb = new StringBuilder("BabyActivity{");
      sb.append("id='").append(id).append('\'')
              .append(", category='").append(category).append('\'');
      switch (category) {
          case "SLEEPING":
          case "EXERCISING":
              sb.append(", duration=").append(duration)
                      .append("m").append('\'');
              break;
          case "EATING":
              sb.append(", intake=").append(intake)
                      .append("ml").append('\'');
              break;
          case "POOPING":
              if (poop_check) {
                  sb.append(", pooped").append('\'');
              } else {
                  sb.append(", did not poop").append('\'');
              }
              break;
          case "VITAMINS":
              if (vitamin_check) {
                  sb.append(", took vitamins");
              } else {
                  sb.append(", did not take vitamins");
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
