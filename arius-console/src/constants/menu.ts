const pkgJson = require('../../package');
export const systemKey = pkgJson.ident;
export const urlPrefix = '/' + systemKey;

export const leftMenus = {
  name: `${systemKey}`,
  path: 'main',
  icon: '#icon-jiqun',
  children: [
    {
      name: 'cluster',
      path: 'cluster',
      icon: '#icon-jiqun1',
      children: [
        {
          name: 'physics',
          path: 'physics',
          icon: '#icon-luoji',
        }, {
          name: 'logic',
          path: 'logic',
          icon: '#icon-jiqun1',
        }, {
          name: 'edition',
          path: 'edition',
          icon: '#icon-jiqun1',
        }],
    }, 
    // 注释调索引
    // {
    //   name: 'index',
    //   path: 'index',
    //   icon: '#icon-jiqun1',
    //   children: [
    //     {
    //       name: 'logic',
    //       path: 'logic',
    //       icon: '#icon-luoji',
    //     }, {
    //       name: 'physics',
    //       path: 'physics',
    //       icon: '#icon-jiqun1',
    //     }],
    // },
    {
      name: 'index-tpl-management',
      path: 'index-tpl-management',
      icon: '#icon-jiqun1',
    },
    {
      name: 'index-admin',
      path: 'index-admin',
      icon: '#icon-jiqun1',
    },
    {
      name: 'search-query',
      path: 'search-query',
      icon: '#icon-jiqun1',
      children: [
        {
          name: 'index-search',
          path: 'index-search',
          icon: '#icon-luoji',
        },
        {
          name: 'dsl-tpl',
          path: 'dsl-tpl',
          icon: '#icon-luoji',
        },
      ],
    },
    {
      name: 'indicators',
      path: 'indicators',
      icon: '#icon-jiqun1',
      children: [
        {
          name: 'cluster',
          path: 'cluster',
          icon: '#icon-luoji',
        }, {
          name: 'gateway',
          path: 'gateway',
          icon: '#icon-luoji',
        }
      ]
    },
    // {
    //   name: 'user',
    //   path: 'user',
    //   icon: '#icon-jiqun1',
    //   children: [
    //     {
    //       name: 'users',
    //       path: 'users',
    //       icon: '#icon-luoji',
    //     }, {
    //       name: 'project',
    //       path: 'project',
    //       icon: '#icon-luoji',
    //     }, {
    //       name: 'role',
    //       path: 'role',
    //       icon: '#icon-luoji',
    //     }],
    // },
    {
      name: 'work-order',
      path: 'work-order',
      icon: '#icon-jiqun1',
      children: [
        {
          name: 'my-application',
          path: 'my-application',
          icon: '#icon-luoji',
        }, {
          name: 'my-approval',
          path: 'my-approval',
          icon: '#icon-luoji',
        }, {
          name: 'task',
          path: 'task',
          icon: '#icon-luoji',
        }],
    },
    {
      name: 'scheduling',
      path: 'scheduling',
      icon: '#icon-jiqun1',
      children: [
        {
          name: 'task',
          path: 'task',
          icon: '#icon-luoji',
        }, {
          name: 'log',
          path: 'log',
          icon: '#icon-luoji',
        }],
    },
    {
      name: 'system',
      path: 'system',
      icon: '#icon-jiqun1',
      children: [
        {
          name: 'config',
          path: 'config',
          icon: '#icon-luoji',
        }, {
          name: 'operation',
          path: 'operation',
          icon: '#icon-luoji',
        }],
    }
  ],
};
