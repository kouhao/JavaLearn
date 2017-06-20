package socket.s6;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Date;

public class Persistence {
	public static void main(String[] args) {
		Persistence.savePerson();
		Persistence.getPerson();
	}

	public static void getPerson() {
		try {
			InputStream in = new FileInputStream("d:\\person.dat");
			ObjectInputStream dataInput = new ObjectInputStream(in);
			Person p = (Person) dataInput.readObject();
			System.out.println(p.getName());
			System.out.println(p.getTall());
			System.out.println(p.getBirthday());
			System.out.println(p.getAddress().getCity());
			System.out.println(p.getAddress().getStreet());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void savePerson() {
		Person p = new Person();
		p.setName("corey");
		p.setTall("171");
		p.setBirthday(new Date());
		p.setAddress(new Address("yiyang", "ziyang"));
		OutputStream out = new ByteArrayOutputStream();
		try {
			OutputStream fileOut = new FileOutputStream(new File("D:\\person.dat"));
			ObjectOutputStream dataOut = new ObjectOutputStream(fileOut);
			dataOut.writeObject(p);
			dataOut.close();
			fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
