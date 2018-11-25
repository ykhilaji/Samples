logger = require('log')
vshard = require('vshard')
cfg = require('shardcfg')

local names = {
    ['1'] = '8a274925-a26d-47fc-9e1b-af88ce939412',
    ['2'] = '3de2e3e1-9ebe-4d0d-abb1-26d301b84633',
    ['3'] = '1e02ae8a-afc0-4e91-ba34-843a356b8ed7',
    ['4'] = '001688c3-66f8-4a31-8e19-036c17d489c2',
}

logger.info('Starting storage: ' .. names[arg[1]])
vshard.storage.cfg(cfg, names[arg[1]])

box.once("vshardtest", function()
    logger.info('Creating user')
    box.schema.user.create('storage', {password='storage'})
    box.schema.user.grant('storage', 'universe')
    logger.info('Creating space')
    local test = box.schema.space.create('test')
    customer:format({
        {'id', 'unsigned'},
        {'bucket_id', 'unsigned'}, -- mandatory field
        {'value', 'string'},
    })

    logger.info('Creating indexes')
    test:create_index('id', {parts = {'id'}})
    test:create_index('bucket_id', {parts = {'bucket_id'}, unique = false}) -- mandatory index
end)