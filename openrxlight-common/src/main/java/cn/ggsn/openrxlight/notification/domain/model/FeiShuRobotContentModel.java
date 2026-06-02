package cn.ggsn.openrxlight.notification.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zh
 *         飞书群 机器人
 *         <p>
 *         https://open.feishu.cn/document/ukTMukTMukTM/ucTM5YjL3ETO24yNxkjN#756b882f
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeiShuRobotContentModel implements ContentModel {

    private String sendType; // 发送类型, 参看SendMessageType
    private String content; // 发送内容
    private String title; // 发送标题
    private String mediaId; // 媒体Id
    /**
     * 富文本内容：[[{"tag":"text","text":"项目有更新:
     * "},{"tag":"a","text":"请查看","href":"http://www.example.com/"},{"tag":"at","user_id":"ou_18eac8********17ad4f02e8bbbb"}]]
     */
    private String postContent;

    private String urlParam; // url上的参数{"stationName":"测试站点"}

}
