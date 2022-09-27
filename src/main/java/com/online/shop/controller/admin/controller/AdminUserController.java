package com.online.shop.controller.admin.controller;

import com.online.shop.controller.BaseController;
import com.online.shop.controller.admin.forms.UserForm;
import com.online.shop.dto.UserDto;
import com.online.shop.entity.Role;
import com.online.shop.entity.User;
import com.online.shop.service.impl.UserServiceImpl;
import com.online.shop.service.util.Pagination;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

import static com.online.shop.service.abstraction.BaseService.INFO_MESSAGE_ATTRIBUTE_NAME;
import static com.online.shop.service.abstraction.BaseService.PAGE_NUMBERS_ATTRIBUTE_NAME;
import static com.online.shop.service.impl.UserServiceImpl.USER_PAGE_ATTRIBUTE_NAME;

@Controller
@RequestMapping(path = "/admin/user")
public class AdminUserController extends BaseController {

    protected AdminUserController(UserServiceImpl userService) {
        super(userService, LoggerFactory.getLogger(AdminUserController.class));
    }

    @GetMapping
    public String getAllUsers(Model model,
                              @RequestParam("page") Optional<Integer> page,
                              @RequestParam("size") Optional<Integer> size) {
        addAuthAttribute(model);
        Pagination<UserDto> pagination = userService.findPaginated(size.orElse(userService.getPageSizeProp()),
                page.orElse(1));
        model.addAttribute(USER_PAGE_ATTRIBUTE_NAME, pagination.page);
        model.addAttribute(PAGE_NUMBERS_ATTRIBUTE_NAME, pagination.pageNumbers);
        return "adminUsers";
    }

    @GetMapping("/{id}")
    public String getSingleUser(Model model,
                                @PathVariable Long id) {
        addAuthAttribute(model);
        model.addAttribute(UserServiceImpl.ROLE_ATTRIBUTE_NAME, Role.getRoles());
        model.addAttribute(UserServiceImpl.SINGLE_USER_ATTRIBUTE_NAME, userService.getUserDtoById(id));
        return "adminUsers";
    }

    @PutMapping("/{id}")
    public String updateUser(Model model,
                             @PathVariable Long id,
                             UserForm userForm) {
        addAuthAttribute(model);
        User currentUser = getUserFromAuthentication();
        model.addAttribute(UserServiceImpl.ROLE_ATTRIBUTE_NAME, Role.getRoles());
        model.addAttribute(UserServiceImpl.SINGLE_USER_ATTRIBUTE_NAME,
                userService.updateUserRole(id, currentUser.getUsername(), userForm.getRole()));
        model.addAttribute(INFO_MESSAGE_ATTRIBUTE_NAME,
                "User is successfully updated.");
        return "adminUsers";
    }

    @PatchMapping("/{id}")
    public String disableOrEnableUser(Model model,
                                      @PathVariable Long id,
                                      @RequestParam(name = "enabled", defaultValue = "true") boolean enabled) {
        addAuthAttribute(model);
        User currentUser = getUserFromAuthentication();
        model.addAttribute(UserServiceImpl.ROLE_ATTRIBUTE_NAME, Role.getRoles());
        model.addAttribute(UserServiceImpl.SINGLE_USER_ATTRIBUTE_NAME,
                userService.setEnabledToUser(id, currentUser.getUsername(), enabled));
        model.addAttribute(INFO_MESSAGE_ATTRIBUTE_NAME,
                "User account state is changed successfully.");
        return "adminUsers";
    }

    @PostMapping("/{id}")
    public String verifyUserManually(Model model,
                                     @PathVariable Long id) {
        addAuthAttribute(model);
        model.addAttribute(UserServiceImpl.ROLE_ATTRIBUTE_NAME, Role.getRoles());
        model.addAttribute(UserServiceImpl.SINGLE_USER_ATTRIBUTE_NAME, userService.verifyUserManually(id));
        model.addAttribute(INFO_MESSAGE_ATTRIBUTE_NAME,
                "User account is verified.");
        return "adminUsers";
    }

}
