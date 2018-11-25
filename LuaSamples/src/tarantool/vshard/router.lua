logger = require('log')
vshard = require('vshard')
cfg = require('shardcfg')

logger.info('Starting router listens on port: ' .. arg[1])
cfg.listen = arg[1]
vshard = require('vshard')
vshard.router.cfg(cfg)