package com.example.demo.entity;

import com.example.demo.entity.UserData;
import jakarta.persistence.*;

@Entity
@Table(
        name = "parking_spots",
        uniqueConstraints = @UniqueConstraint(columnNames = {"lat", "lng"})
)
public class ParkingSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "lat", column = @Column(name = "lat")),
            @AttributeOverride(name = "lng", column = @Column(name = "lng"))
    })
    private Cords cords;

    private String description;
    private boolean canEdit;

    @Enumerated(EnumType.STRING)
    private ParkingSpotState state;

    /** кто создал */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_id")
    private UserData creator;

    /** кто сейчас занял */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "occupied_by")
    private UserData userOccupied;

    public ParkingSpot() {}

    public ParkingSpot(Cords cords, String description, UserData creator) {
        this.cords = cords;
        this.description = description;
        this.creator = creator;
        this.state = ParkingSpotState.FREE;
    }

    // getters / setters
    public Long getId() { return id; }
    public Cords getCords() { return cords; }
    public String getDescription() { return description; }
    public ParkingSpotState getState() { return state; }
    public UserData getCreator() { return creator; }
    public UserData getUserOccupied() { return userOccupied; }

    public void setState(ParkingSpotState state) { this.state = state; }
    public void setUserOccupied(UserData userOccupied) { this.userOccupied = userOccupied; }
    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public void setCords(Cords cords) {
        this.cords = cords;
    }
    public UserData getOwner() {
        return this.creator;
    }
}

