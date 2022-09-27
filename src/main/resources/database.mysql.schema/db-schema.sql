-- CREATE DATABASE apm_tool;
-- CREATE USER 'apm_tool'@'%' IDENTIFIED BY '__SEE_KEEPER__';
-- GRANT ALL PRIVILEGES ON apm_tool.* TO 'apm_tool'@'%';


create table products (
    id int not null auto_increment,
    name varchar(512) not null,
    product_key varchar(256) not null,
    description text,
    primary key (id)
);
create unique index products_key_uidx on products(product_key);

create table product_categories (
    id int not null auto_increment,
    product_id int not null,
    name varchar(512) not null,
    category_key varchar(256) not null,
    description text,
    active boolean not null default true,
    automated boolean not null default false,
    primary key (id),
    foreign key(product_id) references products (id)
);
create unique index product_category_key_uidx on product_categories(product_id, category_key);

create table permission_requests (
    id int not null auto_increment,
    request_from varchar(512) not null,
    request_for varchar(512) not null,
    request_date bigint not null,
    resolution varchar(256),
    resolution_comment text,
    validto_date bigint,
    primary key (id)
);

create table permission_request_to_product (
    product_id int not null,
    permission_request_id int not null,
    foreign key(product_id) references products (id),
    foreign key(permission_request_id) references permission_requests (id)
);

create table permission_request_subcategories (
    id int not null auto_increment,
    product_category_id int,
    permission_request_id int not null,
    access_mode varchar(512),
    comment text,
    foreign key(product_category_id) references product_categories (id),
    foreign key(permission_request_id) references permission_requests (id),
    primary key (id)
);

create table users (
    id int not null auto_increment,
    name varchar(512),
    email varchar(512) not null,
    locale varchar(16) not null default 'de',
    primary key (id)
);
create unique index user_email_uidx on users(email);

create table user_to_product (
    user_id int not null,
    product_id int not null,
    assignment_date bigint not null default 0,
    validto_date bigint,
    foreign key(user_id) references users (id),
    foreign key(product_id) references products (id),
    primary key(user_id, product_id)
);

create table user_to_subcategory (
    user_id int not null,
    category_id int not null,
    assignment_date bigint not null default 0,
    update_date bigint not null default 0,
    validto_date bigint,
    comment text,
    access_mode varchar(512),
    foreign key(user_id) references users (id),
    foreign key(category_id) references product_categories (id),
    primary key(user_id, category_id)
);

create table audit_log (
    id int not null auto_increment,
    date bigint not null,
    action varchar(512) not null,
    user varchar(512) not null,
    target_user varchar(512),
    description text,
    comment text,
    primary key (id)
);

create table connection_data (
    connection_key varchar(64) not null,
    access_token varchar(4096),
    refresh_token varchar(4096),
    token_type varchar(128),
    expires_in int not null default 0,
    creation_date bigint not null default 0,
    primary key(connection_key)
);
