package net.awslink.portal.repository.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "apps")
data class App(
    @Id
    val uuid: UUID? = null,
    val name: String,
    val description: String? = null,
    val iconUrl: String? = null
) {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_category_id")
    var appCategory: AppCategory? = null

    @ManyToMany
    @JoinTable(
        name = "apps_user_groups",
        joinColumns = [JoinColumn(name = "app_id")],
        inverseJoinColumns = [JoinColumn(name = "user_group_id")]
    )
    var requiredUserGroups: MutableSet<UserGroup> = mutableSetOf()
}