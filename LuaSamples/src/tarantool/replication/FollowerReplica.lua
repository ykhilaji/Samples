logger = require('log')
fiber = require('fiber')

box.cfg {
    listen = 3302,
    read_only = true,
    replication = {
        'replicator:pass@leader:3301',
        'replicator:pass@follower:3302'
    }
}

box.once('init', function()
    logger.info('Init follower replica')
    logger.info('Create user for replication')
    box.schema.user.create('replicator', { password = 'pass' })
    box.schema.user.grant('replicator', 'replication')
    logger.info('Create space')
    box.schema.create_space('test', { if_not_exists = true })
    logger.info('Create index')
    box.space.test:create_index('primary', { if_not_exists = true, type = 'HASH', unique = true, parts = { 1, 'scalar' } })
end)

local function readSpace()
    while true do
        logger.info(box.space.test:select())
        fiber.sleep(3)
    end
end

fiber.create(readSpace)
