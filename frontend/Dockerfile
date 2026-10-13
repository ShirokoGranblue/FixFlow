#stage 1 构建依赖和dist
FROM node:24-alpine AS builder

WORKDIR /build

COPY package*.json ./

RUN npm ci

COPY . .

RUN npm run build


#stage 2 nginx
FROM nginx:1.30-alpine

COPY --from=builder /build/dist/ /usr/share/nginx/html/
COPY default.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD [ "nginx","-g","daemon off;" ]