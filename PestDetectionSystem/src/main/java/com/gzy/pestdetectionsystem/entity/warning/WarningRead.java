package com.gzy.pestdetectionsystem.entity.warning;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("warning_read")
public class WarningRead {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long warningId;

    private Long userId;

    private Long readTime;
}
