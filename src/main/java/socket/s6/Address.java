package socket.s6;

public class Address implements java.io.Serializable {
	private static final long serialVersionUID = -6359522147880180316L;
	String city;
	String street;

	Address(String city, String street) {
		this.city = city;
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

}
