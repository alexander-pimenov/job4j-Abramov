package ru.job4j.crud.controller;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import ru.job4j.crud.logic.SecurityService;
import ru.job4j.crud.logic.SecurityServiceImpl;
import ru.job4j.crud.logic.ServletUtil;
import ru.job4j.crud.logic.ServletUtilImpl;
import ru.job4j.crud.logic.Validate;
import ru.job4j.crud.logic.ValidateService;
import ru.job4j.crud.model.Role;
import ru.job4j.crud.model.User;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

/**
 * @author Alexander Abramov (alllexe@mail.ru)
 * @version 1
 * @since 04.10.2019
 */
public class UserUpdateController extends HttpServlet {

    private final Validate logic = ValidateService.getInstance();
    private final ServletUtil servletUtil = ServletUtilImpl.getInstance();
    private final SecurityService securityService = SecurityServiceImpl.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User loggedUser = securityService.getLoggedUser(request.getSession());

        int userId = Integer.parseInt(request.getParameter("id"));
        User user = logic.findById(userId);

        if (user != null) {
            request.setAttribute("action", "edit");
            request.setAttribute("title", "Edit user");
            request.setAttribute("user", user);
            request.setAttribute("buttonName", "Edit user");
            request.setAttribute("roles", loggedUser.isAdmin() ? Role.values() : securityService.getLoggedUserAvaliableRoles(request.getSession()));
            getServletContext().getRequestDispatcher("/WEB-INF/views/user.jsp").forward(request, response);
        } else {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().append("user with id ").append(String.valueOf(userId)).append(" not found");
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        User loggedUser = securityService.getLoggedUser(request.getSession());

        try {
            Map<String, String> params = servletUtil.getPostParams(request);

            User user = new User.Builder()
                    .withId(Integer.parseInt(params.get("id")))
                    .withName(params.get("name"))
                    .withLogin(params.get("login"))
                    .withEmail(params.get("email"))
                    .withPassword(params.get("password"))
                    .withCreateDate(LocalDate.now())
                    .withRole(Role.valueOf(params.get("role")))
                    .build();

            if (!loggedUser.isAdmin()) {
                user.setRole(loggedUser.getRole());
            }
            FileItem fileItem = servletUtil.getUploadedFileFromPostParametrs(request);
            String uploadPath = servletUtil.getUploadPath(request);

            if (logic.update(user, fileItem, uploadPath)) {
                response.sendRedirect("/");
            } else {
                response.setContentType("text/html;charset=utf-8");
                response.getWriter().append("error update user with id ").append(user.getId().toString()).append(" not found");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (FileUploadException e) {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().append("error ").append(e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}