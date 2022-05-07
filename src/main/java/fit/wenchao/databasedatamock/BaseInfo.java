package fit.wenchao.databasedatamock;

import fit.wenchao.databasedatamock.constant.AppendEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
public class BaseInfo {
    String base; AppendEnum appendStrategy; int appendLen;
    Integer counter =0 ;
    public BaseInfo(String base, AppendEnum appendStrategy, int appendLen) {
        this.base = base;
        this.appendStrategy = appendStrategy;
        this.appendLen = appendLen;
    }
}
