package cmmn;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity(name = "Wall")
@Table(name = "wall")
public class Wall {
	@ManyToOne()
	@JoinColumns(value = { @JoinColumn(name = "house_id", referencedColumnName = "id") })
	private House house = null;
	@Id()
	@GeneratedValue()
	private String id = null;
	@OneToOne(mappedBy = "wall")
	private cmmn.WallPlan wallPlan = null;
	String path;
	@javax.persistence.Basic()
	private String uuid = getUuid();

	public Wall() {
	}

	public Wall(cmmn.House owner) {
		this.setHouse(owner);
	}

	public cmmn.House getHouse() {
		cmmn.House result = this.house;
		return result;
	}

	public String getId() {
		String result = this.id;
		return result;
	}

	public cmmn.WallPlan getWallPlan() {
		cmmn.WallPlan result = this.wallPlan;
		return result;
	}

	public void setHouse(cmmn.House newHouse) {
		if (!(newHouse == null)) {
			newHouse.getWalls().add(this);
		} else {
			if (!(this.house == null)) {
				this.house.getWalls().remove(this);
			}
		}
		this.house = newHouse;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setWallPlan(cmmn.WallPlan newWallPlan) {
		cmmn.WallPlan oldValue = this.wallPlan;
		if ((newWallPlan == null || !(newWallPlan.equals(oldValue)))) {
			this.wallPlan = newWallPlan;
			if (!(oldValue == null)) {
				oldValue.setWall(null);
			}
			if (!(newWallPlan == null)) {
				if (!(this.equals(newWallPlan.getWall()))) {
					newWallPlan.setWall(this);
				}
			}
		}
	}

	public void zz_internalSetHouse(cmmn.House value) {
		this.house = value;
	}

	public void zz_internalSetWallPlan(cmmn.WallPlan value) {
		this.wallPlan = value;
	}

	public String getPath() {
		return this.path;
	}

	public void setPath(String value) {
		this.path = value;
	}

	public int hashCode() {
		return getUuid().hashCode();
	}

	public boolean equals(Object o) {
		return o instanceof Wall && ((Wall) o).getUuid().equals(getUuid());
	}

	public String getUuid() {
		if (uuid == null) {
			uuid = java.util.UUID.randomUUID().toString();
		}
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
