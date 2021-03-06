/*
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.dromara.soul.web.filter;

import org.apache.commons.lang3.StringUtils;
import org.dromara.soul.common.constant.Constants;
import org.dromara.soul.common.result.SoulResult;
import org.dromara.soul.common.utils.DateUtils;
import org.dromara.soul.common.utils.GSONUtils;
import org.dromara.soul.web.request.RequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * this is visit time verify filter.
 *
 * @author xiaoyu(Myth)
 */
public class TimeWebFilter extends AbstractWebFilter {

    @Value("${soul.timeVerify.timeDelay:10}")
    private long timeDelay;

    @Override
    protected Mono<Boolean> doFilter(final ServerWebExchange exchange, final WebFilterChain chain) {
        final RequestDTO requestDTO = exchange.getAttribute(Constants.REQUESTDTO);
        assert requestDTO != null;
        final String timestamp = requestDTO.getTimestamp();
        if (StringUtils.isBlank(timestamp)) {
            return Mono.just(false);
        }
        final LocalDateTime start = DateUtils.parseLocalDateTime(timestamp);
        final LocalDateTime now = LocalDateTime.now();
        final long between = DateUtils.acquireMinutesBetween(start, now);
        if (between < timeDelay) {
            return Mono.just(true);
        }
        return Mono.just(false);
    }

    @Override
    protected Mono<Void> doDenyResponse(final ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.REQUEST_TIMEOUT);
        final SoulResult result = SoulResult.error("timestamp is not passed validation");
        return response.writeWith(Mono.just(response.bufferFactory()
                .wrap(GSONUtils.getInstance().toJson(result).getBytes())));
    }
}
