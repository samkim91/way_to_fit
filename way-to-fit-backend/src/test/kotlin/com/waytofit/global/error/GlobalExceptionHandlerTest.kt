package com.waytofit.global.error

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class GlobalExceptionHandlerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @WithMockUser
    fun `존재하지 않는 경로 요청 시 404 응답과 NOT_FOUND 코드를 반환해야 한다`() {
        // given
        val invalidPath = "/api/not-found-path"

        // when & then
        mockMvc.perform(get(invalidPath))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("0002"))
            .andExpect(jsonPath("$.message").value("요청하신 경로를 찾을 수 없습니다: api/not-found-path"))
    }
}
