package com.vialreport.app.presentation.navigation

object Routes {
    const val LOGIN    = "login"
    const val REGISTER = "register"
    const val LIST     = "report_list"
    const val DETAIL   = "report_detail/{id}"
    const val FORM     = "report_form?id={id}"
    const val MAP      = "map"
    const val STATS    = "admin_stats"
    const val PROFILE  = "edit_profile"

    fun detail(id: String): String = "report_detail/$id"
    fun form(id: String?): String = if (id == null) "report_form?id=" else "report_form?id=$id"
}
