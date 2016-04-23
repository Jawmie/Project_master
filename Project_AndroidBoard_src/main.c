/*****************************************************************************
 *
 *   Copyright(C) 2011, Embedded Artists AB
 *   All rights reserved.
 *
 ******************************************************************************
 * Software that is described herein is for illustrative purposes only
 * which provides customers with programming information regarding the
 * products. This software is supplied "AS IS" without any warranties.
 * Embedded Artists AB assumes no responsibility or liability for the
 * use of the software, conveys no license or title under any patent,
 * copyright, or mask work right to the product. Embedded Artists AB
 * reserves the right to make changes in the software without
 * notification. Embedded Artists AB also make no representation or
 * warranty that such application will be suitable for the specified
 * use without further testing or modification.
 *****************************************************************************/

/******************************************************************************
 * Includes
 *****************************************************************************/

#include "lpc17xx_pinsel.h"
#include "lpc17xx_gpio.h"
#include "lpc17xx_ssp.h"
#include "lpc17xx_uart.h"
#include "lpc17xx_timer.h"
#include "lpc17xx_can.h"
#include <string.h>
#include <stdio.h>

#include "board.h"
#include "eeprom.h"
#include "rgb.h"
#include "btn.h"
#include "canpt.h"




/******************************************************************************
 * Forward declarations
 *****************************************************************************/

/******************************************************************************
 * File global variables
 *****************************************************************************/

/******************************************************************************
 * Local functions
 *****************************************************************************/

static uint32_t msTicks = 0;

/******************************************************************************
 * Public functions
 *****************************************************************************/

int main (void)
{

  console_init();
  myblue_init();
  myblue_sendString((uint8_t*)"Herro"); // 8-bit unsigned type
  //console_sendString((uint8_t*)"Demo - AOA Basic\r\n");
  //console_sendString((uint8_t*)"This will hardly work\r\n");

  SysTick_Config(SystemCoreClock / 1000);

  i2c0_init();
  rgb_init();
  trimpot_init();
  btn_init();


  while(1) {
    myblue_sendString((uint8_t*)"Herro\r\n");
  }

}

uint32_t getMsTicks(void)
{
  return msTicks;
}

void SysTick_Handler(void) {
  msTicks++;

}

void check_failed(uint8_t *file, uint32_t line)
{
  /* User can add his own implementation to report the file name and line number,
	 ex: printf("Wrong parameters value: file %s on line %d\r\n", file, line) */

  /* Infinite loop */
  while(1);
}

