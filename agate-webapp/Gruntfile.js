/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

// Generated on 2014-03-27 using generator-jhipster 0.12.0
'use strict';

module.exports = function (grunt) {
  require('load-grunt-tasks')(grunt);
  require('time-grunt')(grunt);

  grunt.initConfig({
    clean: {
      assets: ['src/main/webapp/assets/libs/node_modules'],
    },

    // Put files not handled in other tasks here
    copy: {
      assets: {
        files: [
          {expand: true, src: ['node_modules/admin-lte/dist/**'], dest: 'src/main/webapp/assets/libs/'},
          {expand: true, src: ['node_modules/admin-lte/plugins/bootstrap/**'], dest: 'src/main/webapp/assets/libs/'},
          {expand: true, src: ['node_modules/admin-lte/plugins/chart.js/**'], dest: 'src/main/webapp/assets/libs/'},
          {expand: true, src: ['node_modules/admin-lte/plugins/datatables/**'], dest: 'src/main/webapp/assets/libs/'},
          {expand: true, src: ['node_modules/admin-lte/plugins/datatables-bs4/**'], dest: 'src/main/webapp/assets/libs/'},
          {expand: true, src: ['node_modules/admin-lte/plugins/fontawesome-free/**'], dest: 'src/main/webapp/assets/libs/'},
          {expand: true, src: ['node_modules/admin-lte/plugins/jquery/**'], dest: 'src/main/webapp/assets/libs/'},
          {expand: true, src: ['node_modules/admin-lte/plugins/moment/**'], dest: 'src/main/webapp/assets/libs/'},
          {expand: true, src: ['node_modules/admin-lte/plugins/toastr/**'], dest: 'src/main/webapp/assets/libs/'},
          {expand: true, src: ['node_modules/axios/dist/**'], dest: 'src/main/webapp/assets/libs/'},
          {expand: true, src: ['node_modules/jquery.redirect/**'], dest: 'src/main/webapp/assets/libs/'},
          {expand: true, src: ['node_modules/js-cookie/**'], dest: 'src/main/webapp/assets/libs/'},
          {expand: true, src: ['node_modules/bootstrap-datepicker/dist/**'], dest: 'src/main/webapp/assets/libs/'},
        ]
      }
    },
  });

  grunt.registerTask('build', [
    'clean:assets',
    'copy:assets'
  ]);

  grunt.registerTask('default', [
    'build'
  ]);

};
