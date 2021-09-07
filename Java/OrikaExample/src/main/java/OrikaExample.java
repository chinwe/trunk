import config.OrikaConfig;
import dto.DeviceBaseDto;
import dto.DeviceDto;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import vo.DeviceVo;

@Slf4j
public class OrikaExample {

    public static void main(String[] args) {
        MapperFacade mapperFacade = new OrikaConfig();

        DeviceBaseDto deviceBaseDto = DeviceBaseDto.builder()
                .id("1")
                .name("Device")
                .build();
        final DeviceDto deviceDto = DeviceDto.builder()
                .base(deviceBaseDto)
                .deviceSerial("D12345678")
                .build();

        DeviceVo deviceVo = mapperFacade.map(deviceDto, DeviceVo.class);

        log.info(deviceVo.toString());
    }
}
