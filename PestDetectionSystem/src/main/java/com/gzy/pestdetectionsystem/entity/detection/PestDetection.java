package com.gzy.pestdetectionsystem.entity.detection;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("detection_record")
@Schema(description = "病虫害检测记录")
public class PestDetection {

    @TableId(type = IdType.INPUT)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 原始图片访问地址 */
    private String imageUrl;

    /** 原始文件名 */
    private String originalFileName;

    /** 模型完整识别结果JSON */
    private String resultJson;

    /** 最高置信度标签 */
    private String topLabel;

    /** 置信度 */
    private BigDecimal confidence;

    /** 记录状态：0失败，1成功 */
    
    private Integer status;

    /** 创建时间戳（毫秒） */
    private Long createdTime;

    /** 更新时间戳（毫秒） */
    private Long updatedTime;
}
