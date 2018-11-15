---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by grifon.
--- DateTime: 16/11/2018 00:04
---

logger = require('log')

box.cfg {
    wal_mode = 'none',
    checkpoint_interval = 0,
    log_level = 5
}

box.once('init', function()
    logger.info('Creating index')
    box.schema.sequence.create('id', { start = 0, min = 0, step = 1, cycle = false })
    logger.info('Creating space')
    box.schema.space.create('sample', { if_not_exists = false, engine = 'memtx' })
    logger.info('Creating primary index')
    box.space.sample:create_index('primary', { sequence = 'id', type = 'HASH', unique = true, if_not_exists = true })
end)

local function get(req)
    logger.info('GET')
    local id = tonumber(req:stash('id'))
    local val = box.space.sample:get(id)
    if val == nil then
        return req:render({json = 'value is nil'})
    else
        return req:render({json = val})
    end
end

local function post(req)
    local json = req:json()
    local id = json['id']
    local a = json['a']
    local b = json['b']
    local c = json['c']
    local v = box.space.sample:replace({id, a, b, c})
    return req:render({json = v})
end

local function put(req)
    local json = req:json()
    local a = json['a']
    local b = json['b']
    local c = json['c']
    local v = box.space.sample:insert({nil, a, b, c})
    return req:render({json = v})
end

local function delete(req)
    logger.info('DELETE')
    local id = tonumber(req:stash('id'))
    local val = box.space.sample:get(id)
    if val == nil then
        return req:render({json = 'value is nil'})
    else
        box.space.sample:delete(id)
        return req:render({json = 'ok'})
    end
end

http = require('http.server').new('localhost', 8080)

http:route({path = '/api/get/:id', method = 'GET'}, get)
http:route({path = '/api/post', method = 'POST'}, post)
http:route({path = '/api/put', method = 'PUT'}, put)
http:route({path = '/api/delete/:id', method = 'DELETE'}, delete)

http:start()