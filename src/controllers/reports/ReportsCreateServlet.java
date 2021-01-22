package controllers.reports;

import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import models.Employee;
import models.Report;
import models.validators.ReportValidator;
import utils.DBUtil;

/**
 * Servlet implementation class ReportsCreateServlet
 */
@WebServlet("/reports/create")
@MultipartConfig(location = "C:\\pleiades\\workspace\\daily_report_system\\WebContent\\images\\",fileSizeThreshold = 5000000, maxFileSize = 10000000)
public class ReportsCreateServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ReportsCreateServlet() {
        super();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String _token = (String) request.getParameter("_token");
        if (_token != null && _token.equals(request.getSession().getId())) {
            EntityManager em = DBUtil.createEntityManager();

            Report r = new Report();

            r.setEmployee((Employee) request.getSession().getAttribute("login_employee"));

            Date
            report_date = new Date(System.currentTimeMillis());
            String rd_str = request.getParameter("report_date");
            if (rd_str != null && !rd_str.equals("")) {
                report_date = Date.valueOf(request.getParameter("report_date"));
            }
            r.setReport_date(report_date);

            r.setTitle(request.getParameter("title"));
            r.setContent(request.getParameter("content"));

            Timestamp currentTime = new Timestamp(System.currentTimeMillis());

            //Timestamp型をString型に変換する
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
            String str = sdf.format(currentTime);
            System.out.println(str);
            r.setCreated_at(currentTime);
            r.setUpdated_at(currentTime);

            //取得した画像ファイルをリネームして保存する
            Part part = request.getPart("upfile");
            String Image = this.getfileName(part);
            part.write("C:\\pleiades\\workspace\\daily_report_system\\WebContent\\images\\"+ str + "_" + Image);
            r.setImage_id(str + "_" + Image);


            List<String> errors = ReportValidator.validate(r);
            if (errors.size() > 0) {
                em.close();

                request.setAttribute("_token", request.getSession().getId());
                request.setAttribute("report", r);
                request.setAttribute("errors", errors);

                RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/reports/new.jsp");
                rd.forward(request, response);
            } else {
                em.getTransaction().begin();
                em.persist(r);
                em.getTransaction().commit();
                em.close();
                request.getSession().setAttribute("flush", "登録が完了しました。");

                response.sendRedirect(request.getContextPath() + "/reports/index");
            }

           }

        }


    private String getfileName(Part part) {
        String[] splitedHeader = part.getHeader("Content-Disposition").split(";");

        String Image = null;
        for(String item: splitedHeader) {
            if(item.trim().startsWith("filename")) {
                Image = item.substring(item.indexOf('"')).replaceAll("\"", "");
            }
        }
        return Image;
    }
}