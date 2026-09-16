local redis = require "resty.redis"

local red = redis:new()
red:set_timeout(500)

local ok, connect_error = red:connect("redis", 6379)
local token = ngx.var.http_x_gray_token or ""
local route = "normal"
local decision = "no-token"

if ok and token ~= "" then
    -- 支持两种写法：白名单集合，或单 Token 路由键。
    local whitelisted, whitelist_error = red:sismember("gray:whitelist", token)
    local token_route, token_error = red:get("gray:token:" .. token)

    if whitelisted == 1 or whitelisted == "1" or token_route == "gray" then
        route = "gray"
        decision = "gray-token"
    elseif token_route ~= ngx.null and token_route ~= nil then
        decision = "token-not-gray"
    else
        decision = "not-whitelisted"
    end

    -- 不把用户 Token 写入日志；这里只在学习 Demo 中记录 Redis 查询错误。
    if whitelist_error or token_error then
        ngx.log(ngx.WARN, "gray token lookup failed")
    end
else
    if not ok then
        -- Redis 不可用时默认走 normal，避免灰度判断故障扩大成全站不可用。
        ngx.log(ngx.WARN, "redis unavailable: ", connect_error or "unknown error")
        decision = "redis-unavailable-fallback-normal"
    end
end

if ok then
    red:set_keepalive(10 * 1000, 100)
end

ngx.var.gray_route = route
ngx.var.gray_decision = decision
ngx.req.set_header("X-Gray-Route", route)
ngx.req.set_header("X-Gray-Decision", decision)

if route == "gray" then
    return ngx.exec("@gray")
end

return ngx.exec("@normal")
