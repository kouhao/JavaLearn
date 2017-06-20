package socket.s6;

import java.util.Date;

public class Person implements java.io.Serializable {
	private static final long serialVersionUID = -7604245029519898959L;
	String name;
	String tall;
	transient Date birthday;
	Address address;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTall() {
		return tall;
	}

	public void setTall(String tall) {
		this.tall = tall;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

}
