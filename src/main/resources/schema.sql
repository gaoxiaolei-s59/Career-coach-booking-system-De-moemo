create table bookings
(
    id              bigint auto_increment
        primary key,
    user_id         varchar(64)                           not null comment '用户ID，业务侧传入',
    coach_name      varchar(100)                          null comment '导师姓名',
    coach_email     varchar(100)                          null comment '导师邮箱',
    user_email      varchar(100)                          null comment '用户邮箱',
    start_time      datetime                              not null comment '开始时间',
    end_time        datetime                              not null comment '结束时间',
    cal_booking_uid varchar(100)                          not null comment 'Cal.com的唯一预约ID，用于取消',
    status          varchar(32) default 'PENDING'         not null comment '状态枚举',
    created_at      timestamp   default CURRENT_TIMESTAMP null,
    updated_at      timestamp   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint cal_booking_uid
        unique (cal_booking_uid)
);
