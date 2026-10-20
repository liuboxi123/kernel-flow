package entity.dbEntity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pay")
public class Pay {
    @TableId(type = IdType.AUTO)
    private Long id;
    private int money;
}
