package jerrysun21.nujnah.multi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.provider.Settings.Secure;
import android.util.Log;

public class SecurityHelper {

	public static byte[] generateChecksum(String userdir) {
		File currentFile = new File(userdir);
		List<File> fileList = Arrays.asList(currentFile.listFiles());
		byte[][] checksum = new byte[fileList.size()][];
		byte[] calculatedChecksum = new byte[0];

		for (int i = 0; i < fileList.size(); i++) {
			File temp = fileList.get(i);
			Log.d("jerry", "Computing hash for file: " + temp.getAbsolutePath());
			if (temp.isDirectory()) {
				Log.d("jerry",
						"This is a directory, going into subdirectory "
								+ "\n============================================================");
				checksum[i] = generateChecksum(temp.getAbsolutePath());
			} else if (temp.isFile()) {
				try {
					String data = readFileData(temp);
					Log.d("jerry", "The data in the file: " + data);
					MessageDigest digest1 = MessageDigest.getInstance("SHA");
					digest1.update(data.getBytes("UTF-8"));
					checksum[i] = digest1.digest();
					String checksumString = new String(checksum[i], "UTF-8");
					Log.d("jerry", "The checksum of file: " + checksumString);
				} catch (Exception e) {
					Log.e("jerry", "error reading file");
					e.printStackTrace();
				}
			}
		}

		Log.d("jerry", "Time to combine all the hashes");
		try {
			for (int i = 0; i < fileList.size(); i++) {
				byte[] tempChecksum = new byte[calculatedChecksum.length
						+ checksum[i].length];
				Log.d("jerry", "Length of calculatedChecksum so far: " + calculatedChecksum.length);
				Log.d("jerry", "Length of current   checksum so far: " + checksum[i].length);
				
				for (int j = 0; j < calculatedChecksum.length; j++) {
					tempChecksum[j] = calculatedChecksum[j];
				}
				int offset = calculatedChecksum.length;
				for (int j = offset; j < tempChecksum.length; j++) {
					tempChecksum[j] = checksum[i][j - offset];
				}
				
				MessageDigest digest1;
				digest1 = MessageDigest.getInstance("SHA");
				digest1.update(tempChecksum);
				calculatedChecksum = digest1.digest();
				try {
					String cCString;
					cCString = new String(calculatedChecksum, "UTF-8");
					Log.d("jerry", "Current hash: " + cCString);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return calculatedChecksum;
	}

	public static String readFileData(File file) throws Exception {
		FileInputStream fis = new FileInputStream(file);

		int size = (int) fis.getChannel().size();
		byte[] data = new byte[size];
		fis.read(data, 0, size);
		Log.d("jerry", "reading all data " + size);
		String s = new String(data, "UTF-8");
		fis.close();

		return s;
	}

	public static String decryptFile(String fileName, String password)
			throws Exception {
		Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
		MessageDigest digest1 = MessageDigest.getInstance("SHA");
		digest1.update(password.getBytes());
		// AES requires a key with 128 bits (16 bytes)
		SecretKeySpec key1 = new SecretKeySpec(digest1.digest(), 0, 16, "AES");

		FileInputStream fis = new FileInputStream(fileName);
		aes.init(Cipher.DECRYPT_MODE, key1);

		CipherInputStream cis = new CipherInputStream(fis, aes);
		int size = (int) fis.getChannel().size() - 16;
		byte[] data = new byte[size];
		cis.read(data, 0, size);
		Log.d("jerry", "reading all data " + size);
		String s = new String(data, "UTF-8");
		cis.close();
		fis.close();

		return s;
	}

	public static void encryptFile(String fileName, String dataToEncrypt,
			String password) throws Exception {
		Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
		if (dataToEncrypt.length() % 16 != 0) {
			char pad = (char) (16 - dataToEncrypt.length() % 16);
			for (int i = 0; i < pad; i++) {
				dataToEncrypt += pad;
			}
		} else {
			char pad = (char) 16;
			for (int i = 0; i < pad; i++) {
				dataToEncrypt += pad;
			}
		}

		MessageDigest digest1 = MessageDigest.getInstance("SHA");
		digest1.update(password.getBytes());
		// AES requires a key with 128 bits (16 bytes)
		SecretKeySpec key1 = new SecretKeySpec(digest1.digest(), 0, 16, "AES");
		aes.init(Cipher.ENCRYPT_MODE, key1);

		FileOutputStream fos = new FileOutputStream(new File(fileName));
		Log.d("jerry", "writing all data "
				+ dataToEncrypt.getBytes("UTF-8").length);
		CipherOutputStream cos = new CipherOutputStream(fos, aes);
		cos.write(dataToEncrypt.getBytes("UTF-8"));
		cos.flush();
		fos.flush();
		cos.close();
		fos.close();
	}

	public static void encryptFiles(String userdir, String password) {
		File currentFile = new File(userdir);
		List<File> fileList = Arrays.asList(currentFile.listFiles());

		for (int i = 0; i < fileList.size(); i++) {
			File temp = fileList.get(i);
			if (temp.isDirectory()) {
				encryptFiles(temp.getAbsolutePath(), password);
			} else if (temp.isFile()) {
				String extension = temp.getName();
				Log.d("jerry", "file name: " + extension);
				if (extension.lastIndexOf('-') != -1
						&& extension.substring(extension.lastIndexOf('-'))
								.equals("-sec.txt")) {
					try {
						String data = SecurityHelper.readFileData(temp);
						String newFileName = temp.getAbsolutePath().substring(
								0, temp.getAbsolutePath().lastIndexOf('-'))
								+ ".txt";
						Log.d("jerry", "Data: " + data + "\nName: "
								+ newFileName + "\nPassword: " + password);
						encryptFile(newFileName, data, password);
						temp.delete();
					} catch (Exception e) {
						Log.e("jerry", "error reading file");
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static String setPassword(String user, String nfcData,
			Context context) {
		String password = user;
		password += MultiUserList.getInstance().nfcData;
		password += Secure.getString(context.getContentResolver(),
				Secure.ANDROID_ID);
		Log.d("jerry", "password is " + password);
		return password;
	}
}
