package entity.dbEntity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user") // 指定关联的数据库表名
public class User {
    @TableId(type = IdType.AUTO) // 指定主键生成策略（AUTO 为数据库自增）
    private Long id;
    private String userRole;
}
