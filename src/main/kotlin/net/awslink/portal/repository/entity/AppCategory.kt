package net.awslink.portal.repository.entity

import jakarta.persistence.*

@Entity
@Table(name = "app_categories")
data class AppCategory(
    @Id
    val id: Long? = null,
    val name: String
) {
    @ManyToMany
    @JoinTable(
        name = "app_categories_user_groups",
        joinColumns = [JoinColumn(name = "app_category_id")],
        inverseJoinColumns = [JoinColumn(name = "user_group_id")]
    )
    var requiredUserGroups: MutableSet<UserGroup> = mutableSetOf()

    @OneToMany(mappedBy = "appCategory")
    var apps: MutableSet<App> = mutableSetOf()
}