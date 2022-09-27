
insert into products (name, product_key, description) values('Azure', 'azure', 'Cloud hosting solution');
insert into products (name, product_key, description) values('Azure DevOps', 'azure-devops', 'Projects, pipelines and repos');
insert into products (name, product_key, description) values('Data Center', 'datacenter', 'Legacy datacenter');
insert into products (name, product_key, description) values('Cloudflare', 'cloudflare', 'CDN');
insert into products (name, product_key, description) values('ELK Stack', 'kibana', 'Kibana, cerebro');
insert into products (name, product_key, description) values('Graylog', 'graylog', 'Loganalyzer');
insert into products (name, product_key, description) values('Jenkins', 'jenkins', 'Builds and deployments');
insert into products (name, product_key, description) values('Keeper', 'keeper', 'Distributed password vault');
insert into products (name, product_key, description) values('Wordpress', 'wordpress', 'Some wordpress-sites');

insert into product_categories (name, product_id, category_key, description) select 'dummy-subscription', id, 'dummy-subscription', 'Subscription for digitubbies' from products where product_key = 'azure';
insert into product_categories (name, product_id, category_key, description) select 'zcomment', id, 'CUSTOM', 'custom request comment' from products where product_key = 'azure';

insert into product_categories (name, product_id, category_key, description) select 'zcomment', id, 'CUSTOM', 'custom request comment' from products where product_key = 'azure-devops';

insert into product_categories (name, product_id, category_key, description) select 'zcomment', id, 'CUSTOM', 'custom request comment' from products where product_key = 'datacenter';

insert into product_categories (name, product_id, category_key, description) select 'DNS Settings', id, 'dns', 'DNS Settings' from products where product_key = 'cloudflare';
insert into product_categories (name, product_id, category_key, description) select 'Pagerule Settings', id, 'pagerules', 'Pagerule Settings' from products where product_key = 'cloudflare';
insert into product_categories (name, product_id, category_key, description) select 'Workers', id, 'workers', 'Manage Workers' from products where product_key = 'cloudflare';
insert into product_categories (name, product_id, category_key, description) select 'Cache Purge', id, 'cachepurge', 'Cache Purge' from products where product_key = 'cloudflare';
insert into product_categories (name, product_id, category_key, description) select 'zcomment', id, 'CUSTOM', 'custom request comment' from products where product_key = 'cloudflare';

insert into product_categories (name, product_id, category_key, description) select 'Default Space', id, 'default-space', 'application data from multiple teams' from products where product_key = 'kibana';
insert into product_categories (name, product_id, category_key, description) select 'Reporting Space', id, 'reporting-space', 'dashboards and reportings for non-technical staff' from products where product_key = 'kibana';
insert into product_categories (name, product_id, category_key, description) select 'zcomment', id, 'CUSTOM', 'custom request comment' from products where product_key = 'kibana';

insert into product_categories (name, product_id, category_key, description) select 'Dummy Team', id, 'dummy-team', 'common access' from products where product_key = 'keeper';
insert into product_categories (name, product_id, category_key, description) select 'External Team', id, 'external-team', '' from products where product_key = 'keeper';
insert into product_categories (name, product_id, category_key, description) select 'zcomment', id, 'CUSTOM', 'custom request comment' from products where product_key = 'keeper';

insert into product_categories (name, product_id, category_key, description) select 'zcomment', id, 'CUSTOM', 'custom request comment' from products where product_key = 'jenkins';

insert into product_categories (name, product_id, category_key, description) select 'Dummy Wordpress', id, 'dummy-wordpress', '' from products where product_key = 'wordpress';

insert into product_categories (name, product_id, category_key, description) select 'rev-proxy logs', id, 'revproxy-logs', 'logs from frontend reverse proxy' from products where product_key = 'graylog';
insert into product_categories (name, product_id, category_key, description) select 'zcomment', id, 'CUSTOM', 'custom request comment' from products where product_key = 'graylog';
