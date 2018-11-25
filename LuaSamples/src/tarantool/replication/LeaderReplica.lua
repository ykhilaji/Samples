logger = require('log')
fiber = require('fiber')

box.cfg {
    read_only = false,
    listen = 3301,
    replication = {
        'replicator:pass@leader:3301',
        'replicator:pass@follower:3302'
    }
}

box.once('init', function()
    logger.info('Init master replica')
    logger.info('Create user for replication')
    box.schema.user.create('replicator', { password = 'pass' })
    box.schema.user.grant('replicator', 'replication')
    logger.info('Create space')
    box.schema.create_space('test', { if_not_exists = true })
    logger.info('Create index')
    box.space.test:create_index('primary', { if_not_exists = true, type = 'HASH', unique = true, parts = { 1, 'scalar' } })
end)

local function insertValues()
    local index = 0

    return function()
        while true do
            logger.info('Insert next value: ' .. index)
            box.space.test:insert({ index })
            index = index + 1
            fiber.sleep(3)
        end
    end
end

fiber.create(insertValues())