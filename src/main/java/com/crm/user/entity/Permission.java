package com.crm.user.entity;

public enum Permission {
    // ================= LEAD ASSIGN =================
    LEAD_ASSIGN_CREATE,
    LEAD_ASSIGN_VIEW,
    LEAD_ASSIGN_DELETE,
    LEAD_ASSIGN_REASSIGN,

    // ================= LEAD ACTIVITY =================
    LEAD_ACTIVITY_ADD,
    LEAD_ACTIVITY_VIEW,
    LEAD_ACTIVITY_DELETE,

    // ================= CLIENT =================
    CLIENT_VIEW,
    CLIENT_CREATE,
    CLIENT_UPDATE,
    CLIENT_DELETE,

    // ================= LEAD =================
    LEAD_VIEW,
    LEAD_CREATE,
    LEAD_UPDATE,
    LEAD_DELETE,
    LEAD_CONVERT_TO_CLIENT,

    // ================= LEAD STATUS =================
    LEAD_STATUS_CREATE,
    LEAD_STATUS_VIEW,
    LEAD_STATUS_DELETE,

    // ================= LEAD IMPORT/EXPORT =================
    LEAD_IMPORT,
    LEAD_EXPORT,

    // ================= USER =================
    USER_VIEW,
    USER_CREATE,
    USER_UPDATE,
    USER_DELETE,
    USER_ASSIGN_ROLES,
    USER_MANAGE_PERMISSIONS,
    CLIENT_IMPORT,
    CLIENT_EXPORT,

    // ================= DOCUMENTS =================
    DOCUMENT_UPLOAD,
    DOCUMENT_VIEW,

    // ================= PAYMENTS =================
    PAYMENT_UPLOAD,
    PAYMENT_VIEW,

    // ================= RECEPTION / ATTENDANCE =================
    RECEPTION_CHECK_IN,
    RECEPTION_CHECK_OUT,
    RECEPTION_VIEW_ATTENDANCE,
    RECEPTION_DAILY_REPORT,
    RECEPTION_WEEKLY_REPORT,
    RECEPTION_MONTHLY_REPORT,

    // ================= RECEPTION / VISITS =================
    RECEPTION_SCHEDULE_LEAD,
    RECEPTION_SCHEDULE_CLIENT,
    RECEPTION_MARK_CAME,
    RECEPTION_MARK_MISSED,
    RECEPTION_VIEW_PLANNED,
    RECEPTION_VIEW_VISITS,


    //Kanban
    // 📊 KANBAN (yangi)
    KANBAN_BOARD_CREATE,
    KANBAN_BOARD_VIEW,
    KANBAN_TASK_CREATE,
    KANBAN_TASK_UPDATE,
    KANBAN_TASK_MOVE,
    KANBAN_TASK_DELETE,
    KANBAN_TASK_COMPLETE,
    KANBAN_TASK_VIEW_USER



}
