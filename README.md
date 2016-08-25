EC2-Cost-Optimizer


Aim:
-------------

This application should work as a part of deployment pipeline.in conjuction with CD system.

CD (jenkins/travis) is mainly responsible for delivery(deploy, ui/it/e2e tests) on a PR merge, leaving one 2+ application's stacks:
 one active, one+ for backup and fast rollback

This leads to extra costs for backup stacks.

Aim of this application is to take control on backups's stacks live cycle (clean not-under-traffic stacks up by TTL).


UI:
-------------

application has very basic UI in order to show managed applications with their stacks


Parameters:
-------------

Application is customized by environment variables:

- APP_CACHE_TIMEOUT - timeout cache for application's information calculation, in minutes
- STACK_CLEANUP_IF_ONLY_ONE_LEFT - disable stack cleanup if it's the only one left (even not live), boolean
- APP_INCLUDE: application, whitelist (comma separated). if empty - all apps are whitelisted.
- APP_EXCLUDE: application, blacklist (comma separated). if empty - non apps are blacklisted.
- STACK_NOTRAFFIC_TTL: Stacks ttl, in minutes

Use senza to deploy.