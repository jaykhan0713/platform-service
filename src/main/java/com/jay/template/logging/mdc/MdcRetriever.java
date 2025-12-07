package com.jay.template.logging.mdc;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public final class MdcRetriever {

    private final MdcHeaderProperties props;

    public MdcRetriever(MdcHeaderProperties props){
        this.props = props;
    }

    public String getGatewayTraceId(){
        return MDC.get(props.getxGatewayTraceId());
    }

    public String getUserId() {
        return MDC.get(props.getxUserId());
    }
}
