import java.io.*;

public class Index implements Serializable {
	public static Index load(String filename) {
		FileInputStream fin = null;
		ObjectInputStream oin = null;
		Index result = null;
		try {
			fin = new FileInputStream(filename);
			oin = new ObjectInputStream(fin);
			result = (Index)oin.readObject();
		} catch (IOException e) {
			System.out.println(e);
		} catch (ClassNotFoundException e) {
			System.out.println(e);
		} catch (ClassCastException e) {
			System.out.println(e);
		} finally {
			try {
				if (oin != null)
					oin.close();
				else if (fin != null)
					fin.close();
			} catch (IOException e) {
			}
		}
		return result;
	}

	public void save(String filename) throws IOException {
		FileOutputStream fout = null;
		ObjectOutputStream oout = null;
		try {
			fout = new FileOutputStream(filename);
			oout = new ObjectOutputStream(fout);
			oout.writeObject(this);
		} catch (IOException e) {
			System.out.println(e);
		} finally {
			try {
				if (oout != null)
					oout.close();
				else if (fout != null)
					fout.close();
			} catch (IOException e) {
			}
		}
	}
}
