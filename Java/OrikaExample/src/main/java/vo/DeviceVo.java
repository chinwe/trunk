package vo;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class DeviceVo {
    private DeviceBaseVo base;

    private String deviceSerial;
}
