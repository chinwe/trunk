package com.test.demo;

import com.test.demo.controller.UserController;
import com.test.demo.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author chinwe
 * 2024/10/5
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    public void testGetUserById() throws Exception {
        Mockito.when(userService.getUserById(1)).thenReturn("User1");

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("User1"));
    }

    @Test
    public void testGetUserNotFound() throws Exception {
        Mockito.when(userService.getUserById(2))
                .thenReturn(null);

        mockMvc.perform(get("/users/2"))
                .andExpect(status().isOk());

        String userById = Mockito.verify(userService, Mockito.times(1)).getUserById(2);
        Assertions.assertNull(userById);

    }
}
