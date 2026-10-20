package handle;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;

//处理二进制字节流的servlet不会被过滤
@WebServlet("/index/api/binaryStream")
public class BinaryTask extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long fileId = Long.parseLong(req.getParameter("fileId"));   // 全局分配的唯一的文件ID
        String fileName = "doc";
        String folder = req.getServletContext().getRealPath("/");
        File file = new File(folder + "webapp/HELP/doc.html");
        if (file.exists() && file.isFile()) {
            resp.setHeader("Content-Type", "application/octet-stream");

            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            resp.setHeader("Content-Disposition", "attachment;filename=\"" + encodedFileName + "\";filename*=UTF-8''" + encodedFileName);

            FileInputStream fis = new FileInputStream(file);
            ServletOutputStream os = resp.getOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.close();
            fis.close();
        } else {
            resp.getWriter().write("文件不存在");
        }
    }
}
