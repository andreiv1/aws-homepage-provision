package net.awslink.portal.controller

import net.awslink.portal.constants.URIConstants.API_V1_USERS
import net.awslink.portal.model.UserResponse
import net.awslink.portal.service.PortalUserService
import org.springframework.data.repository.query.Param
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val portalUserService: PortalUserService,
) {
    @GetMapping(API_V1_USERS)
    fun getUsers(
        @Param("sortBy") sortBy: String? = null,
        @Param("sortOrder") sortOrder: String? = null,
    ): ResponseEntity<List<UserResponse>> {

        return ResponseEntity.ok(portalUserService.getAllUsers())
    }
}