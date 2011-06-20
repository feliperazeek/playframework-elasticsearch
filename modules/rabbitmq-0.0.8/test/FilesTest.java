import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Assert;
import org.junit.Test;

import play.exceptions.UnexpectedException;
import play.libs.Files;
import play.test.UnitTest;

public class FilesTest extends UnitTest {

	@Test
	public void testUnzip() throws IOException {
		// Define Temp File
		File tmpFile = File.createTempFile("play.libs.FilesTest.unzip_", ".txt");

		// Define Zip File
		File zipFile = File.createTempFile("play.libs.FilesTest.unzip_", ".zip");

		// Test Files.unzip()
		try {
			// Zip temp location into zipFile defined above
			zip(new File(tmpFile.getParent()), zipFile);

			// Make sure everything is good so far
			Assert.assertTrue(zipFile.exists());
			Assert.assertTrue(zipFile.canRead());

			// Define path where unzipping should happen
			String randomPath = tmpFile.getParent() + File.pathSeparator + "Files_unzipTest_" + new Date().getTime();
			File dest = new File(randomPath);

			// Make sure it dosn't exist yet
			Assert.assertFalse(dest.exists());

			// Unzip temp location into this random path
			Files.unzip(zipFile, dest);

			// Cleanup
			Files.delete(dest);

		} finally {
			// Cleanup
			if ((tmpFile != null) && tmpFile.exists()) {
				tmpFile.delete();
			}
			if ((zipFile != null) && zipFile.exists()) {
				zipFile.delete();
			}
		}
	}

	public static void zip(File directory, File zipFile) {
		try {
			FileOutputStream os = new FileOutputStream(zipFile);
			ZipOutputStream zos = new ZipOutputStream(os);
			zipDirectory(directory, directory, zos);
			zos.close();
			os.close();
		} catch (Exception e) {
			throw new UnexpectedException(e);
		}
	}

	static void zipDirectory(File root, File directory, ZipOutputStream zos) throws Exception {
		for (File item : directory.listFiles()) {
			if (item.isDirectory()) {
				zipDirectory(root, item, zos);
			} else {
				byte[] readBuffer = new byte[2156];
				int bytesIn = 0;
				FileInputStream fis = new FileInputStream(item);
				String path = item.getAbsolutePath().substring(root.getAbsolutePath().length() + 1);
				ZipEntry anEntry = new ZipEntry(path);
				zos.putNextEntry(anEntry);
				while ((bytesIn = fis.read(readBuffer)) != -1) {
					zos.write(readBuffer, 0, bytesIn);
				}
				fis.close();
			}
		}
	}

}
